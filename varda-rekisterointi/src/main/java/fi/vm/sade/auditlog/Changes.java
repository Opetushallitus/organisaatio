package fi.vm.sade.auditlog;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.tananaev.jsonpatch.JsonPatch;
import com.tananaev.jsonpatch.JsonPatchFactory;
import com.tananaev.jsonpatch.JsonPath;
import com.tananaev.jsonpatch.operation.AbsOperation;
import com.tananaev.jsonpatch.operation.AddOperation;
import com.tananaev.jsonpatch.operation.MoveOperation;
import com.tananaev.jsonpatch.operation.ReplaceOperation;

import java.util.Iterator;
import java.util.Map;

public final class Changes {
    public static final Changes EMPTY = new Changes.Builder().build();
    private static final Gson gson = new Gson();
    private static final JsonPatchFactory jsonPatchFactory = new JsonPatchFactory();

    private JsonArray jsonArray = new JsonArray();

    private Changes() { }

    public static <T> Changes addedDto(T dto) {
        return new Changes.Builder().added(gson.toJsonTree(dto)).build();
    }

    public static <T> Changes updatedDto(T dtoAfter, T dtoBefore) {
        JsonElement afterJson = gson.toJsonTree(dtoAfter);
        JsonElement beforeJson = gson.toJsonTree(dtoBefore);
        return new Changes.Builder().jsonDiffToChanges(beforeJson, afterJson).build();
    }

    public static <T> Changes deleteDto(T dto) {
        return new Changes.Builder().removed(gson.toJsonTree(dto)).build();
    }

    public JsonArray asJsonArray() {
        return this.jsonArray;
    }
    public static class Builder {
        private Changes changes;

        public Builder() {
            this.changes = new Changes();
        }

        public Changes build() {
            Changes r = this.changes;
            this.changes = new Changes();
            return r;
        }

        public Builder added(String field, String newValue) {
            return this.added(field,
                    newValue == null ? null : new JsonPrimitive(newValue));
        }

        public Builder added(JsonElement newValue) {
            if (!newValue.isJsonNull()) {
                JsonObject j = new JsonObject();
                j.add("newValue", new JsonPrimitive(toJsonString(newValue)));
                changes.jsonArray.add(j);
            }
            return this;
        }

        public Builder added(String field, JsonElement newValue) {
            if (field == null) {
                throw new IllegalArgumentException("Field name is required");
            }

            JsonObject o = new JsonObject();
            for (int i = 0; i < changes.jsonArray.size(); i++) {
                JsonObject j = changes.jsonArray.get(i).getAsJsonObject();
                if (j.get("fieldName").equals(new JsonPrimitive(field))) {
                    o = j;
                    break;
                }
            }

            if (o.entrySet().size() == 0) {
                o = new JsonObject();
                o.add("fieldName", new JsonPrimitive(field));
                changes.jsonArray.add(o);
            }
            if (newValue == null || newValue.isJsonPrimitive()) {
                o.add("newValue", newValue);
            } else {
                o.add("newValue", new JsonPrimitive(toJsonString(newValue)));
            }
            return this;
        }

        public Builder added(JsonObject newValues) {
            if (newValues != null) {
                for (Map.Entry<String, JsonElement> entry : newValues.entrySet()) {
                    added(entry.getKey(), entry.getValue());
                }
            }
            return this;
        }

        public Builder removed(String field, String oldValue) {
            return this.removed(field,
                    oldValue == null ? null : new JsonPrimitive(oldValue));
        }

        public Builder removed(JsonElement oldValue) {
            if (!oldValue.isJsonNull()) {
                JsonObject j = new JsonObject();
                j.add("oldValue", new JsonPrimitive(toJsonString(oldValue)));
                changes.jsonArray.add(j);
            }
            return this;
        }

        public Builder removed(String field, JsonElement oldValue) {
            if (field == null) {
                throw new IllegalArgumentException("Field name is required");
            }

            JsonObject o = new JsonObject();
            for(int i = 0; i < changes.jsonArray.size(); i++) {
                JsonObject j = changes.jsonArray.get(i).getAsJsonObject();
                if(j.get("fieldName").equals(new JsonPrimitive(field))) {
                    o = j;
                    break;
                }
            }

            if (o.entrySet().size() == 0) {
                o = new JsonObject();
                o.add("fieldName", new JsonPrimitive(field));
                changes.jsonArray.add(o);
            }

            if (oldValue == null || oldValue.isJsonPrimitive()) {
                o.add("oldValue", oldValue);
            } else {
                o.add("oldValue", new JsonPrimitive(toJsonString(oldValue)));
            }
            return this;
        }

        public Builder removed(JsonObject oldValues) {
            if (oldValues != null) {
                for (Map.Entry<String, JsonElement> entry : oldValues.entrySet()) {
                    removed(entry.getKey(), entry.getValue());
                }
            }
            return this;
        }

        public Builder updated(String field, String oldValue, String newValue) {
            return this.updated(
                    field,
                    oldValue == null ? null : new JsonPrimitive(oldValue),
                    newValue == null ? null : new JsonPrimitive(newValue));
        }

        public Builder updated(String field, JsonElement oldValue, JsonElement newValue) {
            if (field == null) {
                throw new IllegalArgumentException("Field name is required");
            }
            if (hasChange(oldValue, newValue)) {
                JsonObject o = new JsonObject();
                o.add("fieldName", new JsonPrimitive(field));
                o.add("oldValue", oldValue);
                o.add("newValue", newValue);
                changes.jsonArray.add(o);
            }
            return this;
        }
        private Builder jsonDiffToChanges(JsonElement beforeJson, JsonElement afterJson) {
            JsonPatch patchArray = jsonPatchFactory.create(beforeJson, afterJson);
            JsonElement current = beforeJson;
            for (Iterator<AbsOperation> it = patchArray.iterator(); it.hasNext(); ) {
                AbsOperation absOperation = it.next();
                String operation = absOperation.getOperationName();
                JsonPath path = absOperation.path;

                String prettyPath = prettify(path);
                switch (operation) {
                    case "add": {
                        added(prettyPath, toJsonString(((AddOperation) absOperation).data));
                        break;
                    }
                    case "remove": {
                        JsonElement oldValue = absOperation.path.navigate(current);
                        removed(prettyPath, toJsonString(oldValue));
                        break;
                    }
                    case "replace": {
                        JsonElement oldValue = absOperation.path.navigate(current);
                        JsonElement newValue = ((ReplaceOperation) absOperation).data;
                        updated(prettyPath, toJsonString(oldValue), toJsonString(newValue));
                        break;
                    }
                    case "move": {
                        JsonPath fromPath = ((MoveOperation) absOperation).from;
                        JsonElement oldValue = fromPath.navigate(current);
                        removed(prettify(fromPath), toJsonString(oldValue));

                        JsonElement newValue = absOperation.path.navigate(afterJson);
                        added(prettyPath, newValue);
                        break;
                    }
                    default: throw new IllegalArgumentException(String.format("Unknown operation %s in %s . before: %s . after: %s"
                            , operation, absOperation.path, beforeJson, afterJson));
                }
                current = absOperation.apply(current);
            }
            return this;
        }

        private boolean hasChange(JsonElement oldValue, JsonElement newValue) {
            return null == oldValue ? null != newValue : !oldValue.equals(newValue);
        }
    }

    private static String prettify(JsonPath path) {
        return path.toString().substring(1).replaceAll("/", ".");
    }

    private static String toJsonString(JsonElement element) {
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        } else if (element.isJsonNull()) {
            return element.toString();
        }
        else {
            return gson.toJson(element);
        }
    }
}

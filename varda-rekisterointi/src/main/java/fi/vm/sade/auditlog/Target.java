package fi.vm.sade.auditlog;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public final class Target {
    private JsonObject json = new JsonObject();

    private Target() { }

    public JsonObject asJson() {
        return this.json;
    }

    public static class Builder {
        private Target target;

        public Builder() {
            this.target = new Target();
        }

        public Target build() {
            Target r = this.target;
            this.target = new Target();
            return r;
        }

        public Builder setField(String name, String value) {
            if (name == null) {
                throw new IllegalArgumentException("Field name is required");
            }
            this.target.json.addProperty(name, value);
            return this;
        }
    }
}

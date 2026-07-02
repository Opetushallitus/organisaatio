export const notIn = (excludedItems: unknown[]) => (item: unknown) => !excludedItems.includes(item);

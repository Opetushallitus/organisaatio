import { Result } from 'axe-core';

export const headingOrderId = 'heading-order';

export function removeExpectViolations(result: Result[], expectedIds: string[]) {
    return result.filter((r) => !expectedIds.includes(r.id));
}

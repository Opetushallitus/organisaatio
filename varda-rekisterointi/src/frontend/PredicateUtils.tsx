export function and<T>(...predicates: ((item: T) => boolean)[]): (item: T) => boolean {
    return function (item: T) {
        return predicates.every((predicate) => predicate(item));
    };
}

export function or<T>(...predicates: ((item: T) => boolean)[]): (item: T) => boolean {
    return function (item: T) {
        return predicates.some((predicate) => predicate(item));
    };
}

import { unstable_createStore } from 'jotai';

type InitialValues = Parameters<typeof unstable_createStore>[0];

export const jotaiStore = unstable_createStore();

export const createJotaiStore = (_initialValues?: InitialValues) => jotaiStore;

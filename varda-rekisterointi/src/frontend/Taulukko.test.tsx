import React from "react";
import {render, unmountComponentAtNode} from 'react-dom';
import {act} from "react-dom/test-utils";
import {Taulukko} from "./Taulukko";

let container: Element;
describe('Taulukko', () => {
    beforeEach(() => {
        container = document.createElement('div');
        document.body.appendChild(container);
    });
    afterEach(() => {
        unmountComponentAtNode(container);
        container.remove();
    });

    it('tulostuu tyhjänä', () => {
        act(() => {
            render(Taulukko({ data: [], sarakkeet: [] }), container);
        });
        expect(container.querySelector("table")).not.toBeNull();
    });

    it('tulostaa rivejä', () => {
        act(() => {
            render(Taulukko({
                data: [{ thingy: "foo" }, { thingy: "bar" }],
                sarakkeet: ["thingy"]
            }), container);
            const table = nonNull(container.querySelector("table"));
            const thead = table.querySelector("thead");
            expect(thead).not.toBeNull();
            const tbody = nonNull(table.querySelector("tbody"));
            expect(tbody).not.toBeNull();
            const bodyRows = tbody.querySelectorAll("tr");
            expect(bodyRows.length).toEqual(2);
        });
    });
});

function nonNull<T>(value: T | null): T {
    return value!;
}

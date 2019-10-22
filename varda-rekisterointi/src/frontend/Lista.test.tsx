import React from "react";
import {render, unmountComponentAtNode} from 'react-dom';
import {act} from "react-dom/test-utils";
import {Lista} from "./Lista";

let container: Element;
describe('Lista', () => {
    beforeEach(() => {
        container = document.createElement('div');
        document.body.appendChild(container);
    });
    afterEach(() => {
        unmountComponentAtNode(container);
        container.remove();
    });

    it('tulostuu tyhjänä', () => {
        const tunnisteGeneraattori = (_: never) => "foo";
        const sarakeGeneraattori = (_: never) => [(<td>foo</td>)];
        act(() => {
            render(Lista({ otsikot: [], rivit: [], sarakeGeneraattori, tunnisteGeneraattori }), container);
        });
        expect(container.querySelector("table.varda-lista")).not.toBeNull();
    });

    it('tulostaa rivejä', () => {
        const otsikot = ['id'];
        const rivit = ['foo', 'bar'];
        const tunnisteGeneraattori = (rivi: string) => rivi;
        const sarakeGeneraattori = (rivi: string) => [(<td>{rivi}</td>)];
        act(() => {
            render(Lista({ otsikot, rivit, sarakeGeneraattori, tunnisteGeneraattori }), container);
        });
        expect(container.querySelectorAll("tr.varda-lista-rivi")).toHaveLength(rivit.length);
    });
});

import React from 'react';
import {render, unmountComponentAtNode} from 'react-dom';
import App from './App';
import {act} from "react-dom/test-utils";

let container: Element;
describe('', () => {
  beforeEach(() => {
    container = document.createElement('div');
    document.body.appendChild(container);
  });
  afterEach(() => {
    act(() => {
      unmountComponentAtNode(container);
    });
    container.remove();
  });

  it('renders without crashing', () => {
    act(() => {
      render(<App />, container);
    });
  });
});

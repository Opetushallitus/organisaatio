const element = document.getElementById('devraamit');
if (!element) {
    const container = document.createElement('div');
    container.id = 'devraamit';
    container.style.cssText = `
            font-size: 14px;
            height: 100px;
            color: white;
            box-sizing: border-box;
            z-index: 100;
            display: flex;
            flex-direction: column;
            background-color: #040066;
            `;
    const headingElement = document.createElement('h4');
    headingElement.append('OrganisaatioPalvelu DEV!');
    const magentaStyleText = 'color: white';
    headingElement.style.cssText = magentaStyleText;
    const a1 = document.createElement('a');
    container.appendChild(headingElement);
    container.appendChild(createLink('/organisaatiot'));
    container.appendChild(createLink('/ryhmat'));
    document.body.prepend(container);
}

function createLink(text) {
    const a = document.createElement('a');
    a.href = text;
    a.text = text;
    a.style.cssText = 'color: rgb(229, 57, 53)';
    return a;
}

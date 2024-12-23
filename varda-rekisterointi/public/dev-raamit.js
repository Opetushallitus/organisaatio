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
    headingElement.append('Virkailijan Rekisterointi DEV!');
    const magentaStyleText = 'color: white';
    const linkstyle = 'color: rgb(229, 57, 53)';
    headingElement.style.cssText = magentaStyleText;
    const a1 = document.createElement('a');
    a1.href = '/varda-rekisterointi/virkailija';
    a1.text = '/etusivu';
    a1.style.cssText = linkstyle;
    container.appendChild(headingElement);
    container.appendChild(a1);
    document.body.prepend(container);
}

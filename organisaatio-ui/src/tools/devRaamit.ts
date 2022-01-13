// this is only for dev purposes to emulate apply-raamit.js principle in cloud environments. Apply-raamit script generates a similar element.

export default function devRaamit(document) {
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
        const linkstyle = 'color: rgb(229, 57, 53)';
        headingElement.style.cssText = magentaStyleText;
        const a1 = document.createElement('a');
        a1.href = '/organisaatiot';
        a1.text = '/organisaatiot';
        const a2 = document.createElement('a');
        a2.href = '/ryhmat';
        a2.text = '/ryhmat';
        a1.style.cssText = linkstyle;
        a2.style.cssText = linkstyle;
        container.appendChild(headingElement);
        container.appendChild(a1);
        container.appendChild(a2);
        document.body.prepend(container);
    }
}

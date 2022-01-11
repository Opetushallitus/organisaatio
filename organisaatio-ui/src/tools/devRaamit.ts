// this is only for dev purposes to emulate apply-raamit.js principle in cloud environments. Apply-raamit script generates a similar element.

export default function devRaamit(document) {
    const element = document.getElementById('devraamit');
    if (!element) {
        const container = document.createElement('div');
        container.id = 'devraamit';
        container.style.cssText = 'height: 5rem;background: blue; display: flex; flex-direction: column; color: white;';
        const headingElement = document.createElement('h4');
        headingElement.append('OrganisaatioPalvelu DEV!');
        const magentaStyleText = 'color: magenta';
        const whiteStyleText = 'color: white';
        headingElement.style.cssText = magentaStyleText;
        const a1 = document.createElement('a');
        a1.href = '/organisaatiot';
        a1.text = '/organisaatiot';
        const a2 = document.createElement('a');
        a2.href = '/ryhmat';
        a2.text = '/ryhmat';
        a1.style.cssText = whiteStyleText;
        a2.style.cssText = whiteStyleText;
        container.appendChild(headingElement);
        container.appendChild(a1);
        container.appendChild(a2);
        document.body.prepend(container);
    }
}

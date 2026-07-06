import { useEffect } from 'react';

export function usePageTitle(title: string) {
    useEffect(() => {
        document.title = title;
    }, [title]);
}

export function useRobotsMeta(content: string) {
    useEffect(() => {
        const selector = 'meta[name="robots"]';
        const existingMeta = document.querySelector<HTMLMetaElement>(selector);
        const meta = existingMeta ?? document.createElement('meta');
        const previousContent = meta.getAttribute('content');

        if (!existingMeta) {
            meta.name = 'robots';
            document.head.appendChild(meta);
        }
        meta.content = content;

        return () => {
            if (!existingMeta) {
                meta.remove();
            } else if (previousContent === null) {
                meta.removeAttribute('content');
            } else {
                meta.content = previousContent;
            }
        };
    }, [content]);
}

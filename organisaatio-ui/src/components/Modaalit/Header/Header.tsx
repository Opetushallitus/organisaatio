import React from 'react';
import { useAtom } from 'jotai';
import { languageAtom } from '../../../api/lokalisaatio';

export default function Header({ label }: { label: string }) {
    const [i18n] = useAtom(languageAtom);
    return <>{i18n.translate(label)}</>;
}

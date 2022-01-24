import React from 'react';
import { languageAtom } from '../../../contexts/LanguageContext';
import { useAtom } from 'jotai';

export default function Header({ headerKey }: { headerKey: string }) {
    const [i18n] = useAtom(languageAtom);
    return <>{i18n.translate(headerKey)}</>;
}

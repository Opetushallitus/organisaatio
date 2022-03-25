import React from 'react';
import { useAtom } from 'jotai';
import { languageAtom } from '../../../api/lokalisaatio';

export default function Header({ headerKey }: { headerKey: string }) {
    const [i18n] = useAtom(languageAtom);
    return <>{i18n.translate(headerKey)}</>;
}

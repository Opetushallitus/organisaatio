import React from 'react';

import { LanguageSwitcher } from './LanguageSwitcher';

type HeaderProps = {
    title: string;
};

export function Header({ title }: HeaderProps) {
    return (
        <header>
            <LanguageSwitcher />
            <div className="content">
                <h1>{title}</h1>
            </div>
        </header>
    );
}

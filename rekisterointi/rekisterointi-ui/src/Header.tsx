import React from 'react';

import { LanguageSwitcher } from './LanguageSwitcher';

type HeaderProps = {
    title: string;
};

export function Header({ title }: HeaderProps) {
    return (
        <header>
            <div className="header">
                <div>
                    <img src="/jotpa_logo.png" alt="Jotpa logo" />
                </div>
                <LanguageSwitcher />
            </div>
            <div className="content">
                <h1>{title}</h1>
            </div>
        </header>
    );
}

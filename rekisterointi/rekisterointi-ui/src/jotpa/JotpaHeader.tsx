import React from 'react';

import { LanguageSwitcher } from '../LanguageSwitcher';

import styles from './jotpa.module.css';

type HeaderProps = {
    title: string;
};

export function Header({ title }: HeaderProps) {
    return (
        <header>
            <div className="header">
                <div>
                    <img className={styles.jotpaLogo} src="/jotpa_logo.png" alt="" />
                </div>
                <LanguageSwitcher />
            </div>
            <div className="content">
                <h1>{title}</h1>
            </div>
        </header>
    );
}

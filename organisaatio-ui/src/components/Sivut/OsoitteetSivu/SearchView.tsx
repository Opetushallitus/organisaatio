import { haeOsoitteet, Hakutulos } from './OsoitteetApi';
import React from 'react';
import css from './OsoitteetSivu.module.css';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Checkbox from '@opetushallitus/virkailija-ui-components/Checkbox';

type SearchViewProps = {
    onResult(result: Hakutulos[]): void;
};
export function SearchView({ onResult }: SearchViewProps) {
    async function hae() {
        const osoitteet = await haeOsoitteet();
        // TODO: Virheilmoitus
        onResult(osoitteet);
    }
    return (
        <>
            <Title>Osoitepalvelu</Title>
            <p>
                Osoitepalveluun kerätään yhteystietoja OPH:n muista palveluista. Yhteystietojen ylläpidosta ja
                ajantasaisuudesta huolehtivat koulutustoimijoiden virkailijat itse.
            </p>
            <div className={css.subtitleRivi}>
                <SubTitle>
                    Valitse ensin haun kohderyhmä <span>(pakollinen)</span>
                </SubTitle>
                {/*<Button variant={'text'}>Tyhjennä valinnat</Button>*/}
            </div>
            <KohderyhmaSelections>
                <KohderyhmaColumn>
                    <Kohderyhma
                        title="Koulutustoimijat"
                        description="Valtion, kunnat, kuntayhtymät, korkeakoulut, yksityiset yhteisöt tai säätiöt"
                        selected={true}
                        disabled={false}
                    />
                </KohderyhmaColumn>
            </KohderyhmaSelections>
            <div className={css.buttonRow}>
                <Button onClick={hae}>Hae</Button>
                {/*<Button variant={'outlined'}>Tyhjennä</Button>*/}
            </div>
        </>
    );
}
function Title(props: React.HTMLAttributes<HTMLHeadingElement>) {
    return <h1 className={css.title}>{props.children}</h1>;
}
function SubTitle(props: React.HTMLAttributes<HTMLHeadingElement>) {
    return <h2 className={css.subtitle}>{props.children}</h2>;
}

function KohderyhmaSelections(props: React.HTMLAttributes<HTMLDivElement>) {
    return <div className={css.kohderyhmaSelections}>{props.children}</div>;
}

function KohderyhmaColumn(props: React.HTMLAttributes<HTMLDivElement>) {
    return <div className={css.kohderyhmaColumn}>{props.children}</div>;
}

type KohderyhmaProps = {
    title: string;
    description: string;
    selected: boolean;
    disabled: boolean;
};

function Kohderyhma({ title, description, selected, disabled }: KohderyhmaProps) {
    const classes = [css.kohderyhma];
    if (disabled) {
        classes.push(css.disabled);
    } else if (selected) {
        classes.push(css.selected);
    }
    return (
        <div className={classes.join(' ')}>
            <div className={css.ylateksti}>
                <Checkbox checked={selected} />
                <h3>{title}</h3>
            </div>
            <Alateksti>{description}</Alateksti>
        </div>
    );
}

function Alateksti(props: React.HTMLAttributes<HTMLParagraphElement>) {
    return <p className={css.alateksti}>{props.children}</p>;
}

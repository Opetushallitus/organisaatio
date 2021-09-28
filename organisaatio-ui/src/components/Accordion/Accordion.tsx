import React, { FormEvent, ReactElement } from 'react';
import styles from './Accordion.module.css';

import {
    Accordion,
    AccordionItem,
    AccordionItemHeading,
    AccordionItemButton,
    AccordionItemPanel,
} from 'react-accessible-accordion';

type props = {
    lomakkeet: ReactElement[];
    otsikot: string[];
    preExpanded?: number | string;
    handlePreExpanded?: (lomakeuuid: string) => void;
    handleItemChange?: (event: FormEvent<HTMLDivElement>) => void;
    handleUuidChange?: (Uuids: string[]) => void;
};

export default function OrganisaatioMuokkausAccordion(props: props) {
    const {
        lomakkeet,
        otsikot,
        preExpanded,
        handleItemChange,
        handlePreExpanded = () => {},
        handleUuidChange = () => {},
    } = props;
    const isPreExpandedInUse = !!preExpanded;
    return (
        <Accordion onChange={handleUuidChange} className={styles.Accordion}>
            {lomakkeet.map((lomake, index) => {
                const uuid = (lomake.key as string) || `${index}`;
                return (
                    <AccordionItem
                        key={lomake.key}
                        {...(isPreExpandedInUse ? { dangerouslySetExpanded: preExpanded === uuid } : {})}
                        onChange={handleItemChange}
                        uuid={uuid}
                        className={styles.AccordionItem}
                    >
                        <AccordionItemHeading
                            className={styles.AccordionHeadingItem}
                            {...(isPreExpandedInUse ? { onClick: () => handlePreExpanded(uuid) } : {})}
                        >
                            <AccordionItemButton className={styles.AccordionButton}>
                                <span className={styles.OtsikkoTeksti}>{`${index + 1}. ${otsikot[index]}`}</span>
                            </AccordionItemButton>
                        </AccordionItemHeading>
                        <AccordionItemPanel className={styles.AccordionPanel}>{lomake}</AccordionItemPanel>
                    </AccordionItem>
                );
            })}
        </Accordion>
    );
}

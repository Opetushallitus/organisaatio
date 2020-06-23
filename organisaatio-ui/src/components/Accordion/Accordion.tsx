import React from 'react';
import styles from './Accordion.module.css';

import {
    Accordion,
    AccordionItem,
    AccordionItemHeading,
    AccordionItemButton,
    AccordionItemPanel,
} from 'react-accessible-accordion';

type props = {
    lomakkeet?: React.ReactNode,
}


export default function OrganisaatioMuokkausAccordion(props: props) {
    const Perustietolomake = props.lomakkeet;
    return (
        <Accordion className={styles.Accordion}>
            <AccordionItem className={styles.AccordionItem}>
                <AccordionItemHeading className={styles.AccordionHeadingItem}>
                    <AccordionItemButton className={styles.AccordionButton}>
                       <span className={styles.OtsikkoTeksti}>1. Perustiedot</span>
                    </AccordionItemButton>
                </AccordionItemHeading>
                <AccordionItemPanel className={styles.AccordionPanel}>
                    {Perustietolomake}
                </AccordionItemPanel>
            </AccordionItem>
            <AccordionItem className={styles.AccordionItem}>
                <AccordionItemHeading className={styles.AccordionHeadingItem}>
                    <AccordionItemButton className={styles.AccordionButton}>
                        1. Perustiedot
                    </AccordionItemButton>
                </AccordionItemHeading>
                <AccordionItemPanel className={styles.AccordionPanel}>
                      sfddfsfdsdfsdfsdfsffsdfsdf
                </AccordionItemPanel>
            </AccordionItem>
        </Accordion>
    );
}
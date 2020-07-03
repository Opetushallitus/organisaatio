import React, {ReactElement} from 'react';
import styles from './Accordion.module.css';

import {
    Accordion,
    AccordionItem,
    AccordionItemHeading,
    AccordionItemButton,
    AccordionItemPanel,
} from 'react-accessible-accordion';

type props = {
    lomakkeet: ReactElement<any>[],
    otsikot: String[],
}


export default function OrganisaatioMuokkausAccordion(props: props) {
    const {lomakkeet, otsikot} = props;
    return (
        <Accordion className={styles.Accordion}>
            {lomakkeet.map((lomake, index) => {
                return (
                    <AccordionItem className={styles.AccordionItem}>
                        <AccordionItemHeading className={styles.AccordionHeadingItem}>
                            <AccordionItemButton className={styles.AccordionButton}>
                                <span className={styles.OtsikkoTeksti}>{`${index + 1}. ${otsikot[index]}`}</span>
                            </AccordionItemButton>
                        </AccordionItemHeading>
                        <AccordionItemPanel className={styles.AccordionPanel}>
                            {lomake}
                        </AccordionItemPanel>
                    </AccordionItem>
                );
            })
            }
        </Accordion>
    );
}
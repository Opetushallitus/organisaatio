import React, {FormEvent, ReactElement, ReactText} from 'react';
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
    preExpanded?: number,
    handlePreExpanded?: (number: number) => void
    handleChange?: (event: FormEvent<HTMLDivElement>) => void
}


export default function OrganisaatioMuokkausAccordion(props: props) {
    const {lomakkeet, otsikot, preExpanded, handleChange, handlePreExpanded = () => {}}  = props;
    console.log('oasddsa', preExpanded)
    const isPreExpandedInUse = !!preExpanded;
    return (
        <Accordion className={styles.Accordion}>
            {lomakkeet.map((lomake, index) => {
                return (
                    <AccordionItem
                        {...(isPreExpandedInUse ? { dangerouslySetExpanded: preExpanded === index } : {})}
                        onChange={handleChange} uuid={`${index}`} className={styles.AccordionItem}>
                        <AccordionItemHeading
                            className={styles.AccordionHeadingItem}
                            {...(isPreExpandedInUse ? { onClick: () => handlePreExpanded(index) } : {})}
                        >
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
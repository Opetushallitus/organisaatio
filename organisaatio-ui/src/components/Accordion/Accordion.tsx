import React, { FormEvent, ReactElement } from 'react';
import styles from './Accordion.module.css';
import {
    Accordion as ReactAccordion,
    AccordionItem,
    AccordionItemButton,
    AccordionItemHeading,
    AccordionItemPanel,
} from 'react-accessible-accordion';

type props = {
    lomakkeet: ReactElement[];
    otsikot: string[];
    preExpanded?: string;
    handlePreExpanded?: (lomakeuuid: string) => void;
    handleItemChange?: (event: FormEvent<HTMLDivElement>) => void;
    handleUuidChange?: (Uuids: string[]) => void;
};

export default function Accordion(props: props) {
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
        <ReactAccordion onChange={handleUuidChange} className={styles.Accordion}>
            {lomakkeet.map((lomake, index) => {
                const id = (lomake.key as string) || `${index}`;
                return (
                    <AccordionItem
                        key={lomake.key}
                        {...(isPreExpandedInUse ? { dangerouslySetExpanded: preExpanded === id } : {})}
                        onChange={handleItemChange}
                        uuid={id}
                        className={styles.AccordionItem}
                    >
                        <AccordionItemHeading
                            className={styles.AccordionHeadingItem}
                            {...(isPreExpandedInUse ? { onClick: () => handlePreExpanded(id) } : {})}
                        >
                            <AccordionItemButton className={styles.AccordionButton}>
                                <span className={styles.OtsikkoTeksti}>{`${index + 1}. ${otsikot[index]}`}</span>
                            </AccordionItemButton>
                        </AccordionItemHeading>
                        {preExpanded === id && (
                            <AccordionItemPanel className={styles.AccordionPanel}>{lomake}</AccordionItemPanel>
                        )}
                    </AccordionItem>
                );
            })}
        </ReactAccordion>
    );
}

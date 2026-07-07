import React, { ReactElement, useState } from 'react';
import styles from './Accordion.module.css';

type Props = {
    lomakkeet: ReactElement[];
    otsikot: string[];
    preExpanded?: string;
    handlePreExpanded?: (lomakeuuid: string) => void;
    handleItemChange?: (lomakeuuid: string) => void;
    handleUuidChange?: (Uuids: string[]) => void;
};

function getLomakeId(lomake: ReactElement, index: number): string {
    return lomake.key ? String(lomake.key) : `${index}`;
}

export default function Accordion(props: Props) {
    const {
        lomakkeet,
        otsikot,
        preExpanded,
        handleItemChange,
        handlePreExpanded = () => {},
        handleUuidChange = () => {},
    } = props;
    const [expandedLomake, setExpandedLomake] = useState<string | undefined>();
    const isControlled = preExpanded !== undefined;
    const selectedLomake = isControlled ? preExpanded : expandedLomake;

    function selectLomake(id: string) {
        handlePreExpanded(id);

        if (selectedLomake === id) {
            return;
        }

        handleItemChange?.(id);
        handleUuidChange([id]);

        if (!isControlled) {
            setExpandedLomake(id);
        }
    }

    return (
        <div className={styles.Accordion}>
            {lomakkeet.map((lomake, index) => {
                const id = getLomakeId(lomake, index);
                const headingId = `accordion__heading-${id}`;
                const panelId = `accordion__panel-${id}`;
                const expanded = selectedLomake === id;

                return (
                    <div key={id} className={styles.AccordionItem}>
                        <div id={headingId} className={styles.AccordionHeadingItem}>
                            <button
                                type="button"
                                className={styles.AccordionButton}
                                aria-expanded={expanded}
                                aria-controls={panelId}
                                onClick={() => selectLomake(id)}
                            >
                                <span className={styles.OtsikkoTeksti}>{`${index + 1}. ${otsikot[index]}`}</span>
                            </button>
                        </div>
                        {expanded && (
                            <div
                                id={panelId}
                                role="region"
                                aria-labelledby={headingId}
                                className={styles.AccordionPanel}
                            >
                                {lomake}
                            </div>
                        )}
                    </div>
                );
            })}
        </div>
    );
}

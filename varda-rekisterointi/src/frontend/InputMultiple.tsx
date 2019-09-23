import React, { useContext } from 'react';
import Button from './Button';
import { LanguageContext } from './contexts';
import styles from './InputMultiple.module.css';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTimes } from '@fortawesome/free-solid-svg-icons';
import classNames from 'classnames/bind';

type Props = {
    values: string[],
    disabled?: boolean,
    onChange: (values: string[]) => void,
}

export default function InputMultiple(props: Props) {
    const { i18n } = useContext(LanguageContext);
    function add(value: string) {
        return [ ...props.values, value ];
    }
    function edit(value: string, index: number): string[] {
        props.values.splice(index, 1, value);
        return [ ...props.values ];
    }
    function remove(index: number) {
        props.values.splice(index, 1);
        return [ ...props.values ];
    }
    return (
        <div>{props.values.map((value, index) => {
            return <div key={index} className={styles.inputContainer}>
                <input className="oph-input"
                       type="text"
                       value={value}
                       disabled={props.disabled}
                       onChange={event => props.onChange(edit(event.currentTarget.value, index))} />
                {props.disabled ? null :
                <button className={classNames("oph-button oph-button-close", styles.removeButton)}
                        type="button"
                        disabled={props.disabled}
                        onClick={() => props.onChange(remove(index))}>
                    <FontAwesomeIcon icon={faTimes} />
                </button>
                }
            </div>
        })}
        {props.disabled ? null :
        <Button type="button"
                disabled={props.disabled}
                styling="ghost"
                onClick={() => props.onChange(add(''))}>{i18n.translate('LISAA_SAHKOPOSTI')}</Button>
        }
        </div>
    )
}

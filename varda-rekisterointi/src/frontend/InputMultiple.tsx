import React, {useContext} from 'react';
import Button from './Button';
import { LanguageContext } from './contexts';
import styles from './InputMultiple.module.css';
import ClearIcon from '@material-ui/icons/Clear';
import classNames from 'classnames';

type Props = {
    id?: string,
    values: string[],
    disabled?: boolean,
    hasError?: boolean,
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
    const classes = classNames({
        'oph-input': true,
        'oph-input-has-error': props.hasError,
    });
    return (
        <div>
            {props.disabled ? null :
                <Button
                    tabIndex={0}
                    type="button"
                    disabled={props.disabled}
                    styling="ghost"
                    className={styles.addButton}
                    onClick={() => props.onChange(add(''))}>{i18n.translate('LISAA_SAHKOPOSTI')}</Button>
            }
            {props.values.map((value, index) => {
            return <div key={index} className={styles.inputContainer}>
                <input tabIndex={0}
                       aria-labelledby={props.id}
                       className={classes}
                       type="text"
                       value={value}
                       readOnly={props.disabled}
                       onChange={event => props.onChange(edit(event.currentTarget.value, index))} />
                {props.disabled ? null :
                <button aria-label={i18n.translate('POISTA_SAHKOPOSTI')}
                        tabIndex={0}
                        className={classNames("oph-button oph-button-close", styles.removeButton)}
                        type="button"
                        disabled={props.disabled}
                        onClick={() => props.onChange(remove(index))}>
                    <ClearIcon />
                </button>
                }
            </div>
        })}
        </div>
    )
}

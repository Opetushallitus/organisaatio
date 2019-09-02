import React, { useContext } from 'react';
import Button from './Button';
import { LanguageContext } from './contexts';

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
            return <div key={index} style={{position: 'relative'}}>
                <input className="oph-input"
                       type="text"
                       value={value}
                       disabled={props.disabled}
                       onChange={event => props.onChange(edit(event.currentTarget.value, index))} />
                <button className="oph-button oph-button-close" style={{padding: '0px'}}
                        type="button"
                        disabled={props.disabled}
                        onClick={() => props.onChange(remove(index))}>
                    <span>x</span>
                </button>
            </div>
        })}
        <Button type="button"
                disabled={props.disabled}
                styling="ghost"
                onClick={() => props.onChange(add(''))}>{i18n.translate('LISAA_SAHKOPOSTI')}</Button>
        </div>
    )
}

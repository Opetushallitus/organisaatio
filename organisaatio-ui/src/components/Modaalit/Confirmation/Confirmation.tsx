import PohjaModaali from '../PohjaModaali/PohjaModaali';
import * as React from 'react';
import Body from './Body';
import Footer from './Footer';
import Header from './Header';

export function Confirmation(props: {
    header: string;
    message: string;
    tallennaCallback: () => void;
    peruutaCallback: () => void;
    suljeCallback: () => void;
}) {
    return (
        <PohjaModaali
            header={<Header headerKey={props.header} />}
            body={<Body messageKey={props.message} />}
            footer={<Footer tallennaCallback={props.tallennaCallback} peruutaCallback={props.peruutaCallback} />}
            suljeCallback={props.suljeCallback}
        />
    );
}

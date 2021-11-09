import * as React from 'react';
import { Kortti } from './Kortti';

export const YhteystiedotKortit = ({ osoitteet }) => {
    const cardsArray = osoitteet.map((robot) => <Kortti name={robot.name} email={robot.email} id={robot.id} />);
    return <div>{cardsArray}</div>;
};

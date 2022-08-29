import axios from 'axios';
import React, { useEffect, useState } from 'react';

function VardaAloitus() {
    const [organisaatiot, setOrganisaatiot] = useState();
    useEffect(() => {
        async function fetchOrganisaatiot() {
            const resp = await axios.get('/hakija/api/organisaatiot');
            setOrganisaatiot(resp.data);
        }
        void fetchOrganisaatiot();
    }, [setOrganisaatiot]);

    if (!organisaatiot) {
        return <div>fetching...</div>;
    }

    return <div>{JSON.stringify(organisaatiot)}</div>;
}

export default VardaAloitus;

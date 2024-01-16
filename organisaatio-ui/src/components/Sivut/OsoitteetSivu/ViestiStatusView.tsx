import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getEmail, QueuedEmail } from './OsoitteetApi';

export const ViestiStatusView = () => {
    const { emailId } = useParams<{ emailId: string }>();
    const [email, setEmail] = useState<QueuedEmail | undefined>(undefined);
    useEffect(() => {
        getEmail(emailId).then((email) => setEmail(email));
    }, [emailId]);

    return (
        <div>
            <pre>{JSON.stringify(email, null, 2)}</pre>
        </div>
    );
};

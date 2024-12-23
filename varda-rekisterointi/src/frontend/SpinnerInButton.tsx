import React from 'react';

export default function SpinnerInButton() {
    return (
        <span className="oph-spinner oph-spinner-in-button">
            <span className="oph-bounce oph-bounce1" aria-hidden="true"></span>
            <span className="oph-bounce oph-bounce2" aria-hidden="true"></span>
            <span className="oph-bounce oph-bounce3" aria-hidden="true"></span>
        </span>
    );
}

import * as React from 'react';

type Props = {
    children: React.ReactNode,
}

export default function ErrorPage(props: Props) {
    return <div>{props.children}</div>;
}

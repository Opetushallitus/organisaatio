import styled from 'styled-components';

import Icon from '../Icon';

const InputIcon = styled(Icon)`
    font-size: 1.25rem;
    color: ${({ theme }) => theme.colors.text.secondary};
`;

export type InputIconProps = React.ComponentProps<typeof InputIcon>;

export default InputIcon;

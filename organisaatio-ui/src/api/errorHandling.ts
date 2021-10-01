import Axios, { AxiosError, AxiosResponse } from 'axios';
import { danger, warning } from '../components/Notification/Notification';
type OrganisaatioVirhe = {
    errorKey: string;
    errorMessage: string;
};
function handleError(error) {
    if (Axios.isAxiosError(error)) {
        const axiosError = error as AxiosError<OrganisaatioVirhe>;
        if (axiosError.response) {
            if (axiosError.response.data.errorKey) {
                warning({ message: axiosError.response.data.errorKey });
                console.error(axiosError.response.data.errorKey, axiosError.response.data.errorMessage);
            } else {
                danger({ message: axiosError.response.statusText });
                console.error(axiosError);
            }
        }
    } else {
        danger({ message: error });
        console.error(error);
    }
}
async function errorHandlingWrapper<A = never, B = AxiosResponse<A>>(workhorse: () => Promise<B>): Promise<B> {
    try {
        return await workhorse();
    } catch (error) {
        handleError(error);
        return new Promise(() => {});
    }
}
function useErrorHandlingWrapper(workhorse) {
    try {
        return workhorse();
    } catch (error) {
        handleError(error);
    }
}
export { errorHandlingWrapper, useErrorHandlingWrapper };

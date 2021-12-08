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
            console.error(axiosError.response);
            if (axiosError.response.data?.errorKey) {
                warning({ title: axiosError.response.data.errorKey, message: axiosError.response.data.errorMessage });
            } else if (axiosError.response.data?.errorMessage) {
                danger({ title: axiosError.response.statusText, message: axiosError.response.data.errorMessage });
            } else {
                danger({ message: axiosError.response.statusText });
            }
        }
    } else {
        danger({ message: error });
        console.error(error);
    }
}
async function errorHandlingWrapper<A = never, B = AxiosResponse<A>>(
    workhorse: () => Promise<B>
): Promise<B | undefined> {
    try {
        const value = await workhorse();
        return value;
    } catch (error) {
        await handleError(error);
        return new Promise((resolve, reject) => resolve(undefined));
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

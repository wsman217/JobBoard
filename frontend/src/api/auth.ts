const API_URL: string | undefined = import.meta.env.VITE_API_URL;
const GOOGLE_AUTHORIZATION: string | undefined = import.meta.env.VITE_GOOGLE_AUTHORIZATION;

const authOrigin = API_URL ? new URL(API_URL).origin : "";

const currentLocation = (): string =>
    `${window.location.pathname}${window.location.search}${window.location.hash}`;

export const googleLoginUrl = (): string => {
    const destination = currentLocation();

    if (!authOrigin) {
        return `${GOOGLE_AUTHORIZATION}?redirect=${encodeURIComponent(destination)}`;
    }

    const url = new URL(`${GOOGLE_AUTHORIZATION}`, authOrigin);
    url.searchParams.set("redirect", destination);
    return url.toString();
};

export const beginGoogleLogin = (): void => {
    window.location.assign(googleLoginUrl());
};
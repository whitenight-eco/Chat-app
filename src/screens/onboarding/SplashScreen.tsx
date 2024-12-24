import React, { useEffect, useRef, useState } from 'react';
import LottieView from 'lottie-react-native';
// import ProfileStore from 'src/store/ProfileStore';
// import MainStore from 'src/store/MainStore';
const SplashLottie = require('src/assets/lottie/splash.json');

interface IProps {
    onFinish?: () => void;
}

const SplashScreen = (props: IProps) => {
    const [loading, setLoading] = useState(true);
    const finished = useRef(false);
    const init = async () => {
        try {
            // await ProfileStore.init();
            // await MainStore.fetchVehicles();
            // await ProfileStore.fetchUser();
            // await MainStore.fetchWarrantyList();
            setLoading(false);
        } catch (e) {
            console.log(e);
            // ProfileStore.logOut();
        }
        if (finished.current) props.onFinish?.();
    }

    useEffect(() => {
        init()
    }, []);

    return (
        <LottieView
            style={{ flex: 1 }}
            resizeMode="cover"
            source={SplashLottie}
            onAnimationFinish={() => {
                finished.current = true;
                props.onFinish?.();
            }}
            autoPlay
            loop={false}
        />
    )
}

export default SplashScreen;
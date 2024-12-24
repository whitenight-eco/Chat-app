import { useNavigation } from "@react-navigation/native";
import { useEffect, useState } from "react";
import { Keyboard } from "react-native";

interface IProps {
    onHide?: () => void;
    onShow?: () => void;
    onChange?: () => void;
}

const useKeyboard = (props?: IProps) => {
    const [showKeyboard, setShowKeyboard] = useState(false);
    const [focusScreen, setFocusScreen] = useState(false);
    const navigation = useNavigation();

    useEffect(() => {
        const listener1 = Keyboard.addListener("keyboardDidShow", () => {
            props?.onShow?.();
            props?.onChange?.();
            setShowKeyboard(true);
        });
        const listener2 = Keyboard.addListener("keyboardDidHide", () => {
            props?.onHide?.();
            props?.onChange?.();
            setShowKeyboard(false);
        });
        return () => {
            listener1.remove();
            listener2.remove();
        }
    }, [focusScreen]);

    useEffect(() => {
        navigation.addListener("focus", () => {
            setTimeout(() => setFocusScreen(false), 100);
        })
    }, [])

    return showKeyboard;
}

export default useKeyboard;
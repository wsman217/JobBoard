import './App.css'
import {useState} from 'react'
import {NavLink, Route, Routes} from 'react-router-dom'
import SelectableInput, {SelectableInputOption} from "./ui-components/SelectableInput";
import Button from "./ui-components/Button";
import CalendarSelector from "./ui-components/CalendarSelector";
import Modal from "./ui-components/Modal";
import TestPage from "./pages/TestPage";

function Home() {
    const [date, setDate] = useState(new Date());

    return (
        <section id="center">
            <div>
                <a href="https://api.int-test.com/oauth2/authorization/google">Sign in with Google</a>
                <br/>
                <a onClick={async () => {
                    const response = await fetch("https://api.int-test.com/api/users/me", {
                        credentials: "include"
                    });
                    const data = await response.json();
                    console.log(data);
                }}>Get user info</a>
            </div>
            <Button onClick={() => console.log("clicked")}>
                Test
            </Button>
            <SelectableInput placeholder={"testing"} setValue={(value: string) => {console.log(value)}}>
                <SelectableInputOption id={"Applied for Job"} value={"applied"}/>
                <SelectableInputOption id={"Submitted Resume"} value={"submitted-resume"}/>
                <SelectableInputOption id={"Job Fair"} value={"job-fair"}/>
                <SelectableInputOption id={"Interview"} value={"interview"}/>
                <SelectableInputOption id={"Test"}/>
                <SelectableInputOption id={"What about a very long input option?"}/>
            </SelectableInput>
            <CalendarSelector value={date} setValue={(value: Date) => setDate(value)} />
        </section>
    );
}

function App() {
    const [signInOpen, setSignInOpen] = useState(false);

    return (
        <>
            <nav className="app-nav">
                <div className="app-nav__links">
                    <NavLink to="/" end>Home</NavLink>
                    <NavLink to="/test-page">Test</NavLink>
                </div>
                <Button onClick={() => setSignInOpen(true)}>Sign in</Button>
            </nav>
            <main className="app-main">
                <Routes>
                    <Route path="/" element={<Home />} />
                    <Route path="/test-page" element={<TestPage />} />
                </Routes>
            </main>
            <Modal open={signInOpen} onClose={() => setSignInOpen(false)} title="Sign in">
                <Button
                    className="sign-in-option"
                    onClick={() => window.location.assign("https://api.int-test.com/oauth2/authorization/google")}
                >
                    Sign in with Google
                </Button>
            </Modal>
        </>
    )
}

export default App
import './App.css'
import SelectableInput, {SelectableInputOption} from "./ui-components/SelectableInput";

function App() {

    return (
        <>
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
                <SelectableInput placeholder={"testing"} setValue={(value: string) => {console.log(value)}}>
                    <SelectableInputOption id={"Applied for Job"} value={"applied"}/>
                    <SelectableInputOption id={"Submitted Resume"} value={"submitted-resume"}/>
                    <SelectableInputOption id={"Job Fair"} value={"job-fair"}/>
                    <SelectableInputOption id={"Interview"} value={"interview"}/>
                    <SelectableInputOption id={"Test"}/>
                    <SelectableInputOption id={"What about a very long input option?"}/>
                </SelectableInput>
            </section>
        </>
    )
}

export default App

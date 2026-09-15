import './App.css'

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
                        const data = response.json();
                        console.log(data);
                    }}>Get user info</a>
                </div>
            </section>
        </>
    )
}

export default App

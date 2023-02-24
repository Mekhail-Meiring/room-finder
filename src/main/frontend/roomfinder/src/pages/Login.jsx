import React from "react";
import loginImg from "../../public/assets/login.svg";
import "./styles/Login.scss";

export class Login extends React.Component {
    constructor(props) {
        super(props);
    }

    onSubmit = async (e) => {
        e.preventDefault();
        const data = new FormData(e.target);
        const id = data.get('client_id').toString();

        const response = await fetch('http://localhost:8080/api/login', {
            method: 'POST',
            body : data
        });

        const json = await response.json();

        if (response.status === 200) {

            const response2 = await fetch(`http://localhost:8080/api/get-profile-pic/${id}`);
            const json2 = await response2.json();

            console.log(json2);

            sessionStorage.setItem("client_id", json.id_number);
            sessionStorage.setItem("client_name", json.name);

            if (json2.photo_url !== ""){
                sessionStorage.setItem("profile_photo", json2.photo_url);
            }

            const url = '/home';
            window.history.pushState(null, '', url);
            window.location.href = url;
        }
    }



    render() {
        return (
            <div className="base-container" ref={this.props.containerRef}>
                <div className="header">Login</div>
                <div className="content">
                    <div className="image">
                        <img src={loginImg}  alt={"login image"}/>
                    </div>
                    <form className="form" onSubmit={this.onSubmit}>
                        <div className="form-group">
                            <input type="text" name="client_id" placeholder="ID Number" />
                        </div>
                        <div className="footer">
                            <button type="submit" className="btn">
                                Login
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        );
    }
}
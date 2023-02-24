import React from "react";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {faUpload} from "@fortawesome/free-solid-svg-icons";
import "./styles/Login.scss";

export class Register extends React.Component {
    constructor(props) {
        super(props);
    }


    uploadImageToBitBucket = async (imageFile) => {

        const response = await fetch('http://localhost:8080/api/s3-url');
        const url = await response.json();

        await fetch(url, {
            method: 'PUT',
            headers: {
                "Content-Type": "multipart/form-data"
            },
            body: imageFile
        });

        return url.split('?')[0];

    }


    onSubmit = async (e) => {
        e.preventDefault();
        const data = new FormData(e.target);

        const id = data.get('id_number');
        const name = data.get('name');
        const surname = data.get('surname');
        const email = data.get('email');
        const phoneNumber = data.get('phone_number');
        const image = data.get('image');

        const response = await fetch('http://localhost:8080/api/register', {
            method: 'POST',
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                id_number: id,
                name: name,
                surname: surname,
                email_address: email,
                phone_number: phoneNumber
            })
        });

        console.log(response.status);

        if (response.status === 201) {
            console.log("User registered successfully");

            let imageUrl = ""

            if (image.size > 0){
                imageUrl = await this.uploadImageToBitBucket(image);
            }

            await fetch('http://localhost:8080/api/upload-profile-pic', {
                method: 'POST',
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    "client_id": id,
                    "photo_url": imageUrl
                })
            });

            const url = '/';
            window.history.pushState(null, '', url);
            window.location.href = url;
        }
    }

    render() {
        return (
            <div className="base-container" ref={this.props.containerRef}>
                <div className="header">Register</div>
                <div className="content">
                    <p>Please provide us with your information</p>
                    <form className="form" onSubmit={this.onSubmit}>

                        <div className="file-input">
                            <input className='file-input-field' type="file" name="image"/>
                            <button className='file-btn'>
                                <i className='file-icon'>
                                    <FontAwesomeIcon icon={faUpload} />
                                </i>
                                Profile Picture
                            </button>
                        </div>

                        <div className="form-group">
                            <input type="text" name="name" placeholder="Name" />
                        </div>
                        <div className="form-group">
                            <input type="text" name="surname" placeholder="surname" />
                        </div>
                        <div className="form-group">
                            <input type="email" name="email" placeholder="email" />
                        </div>
                        <div className="form-group">
                            <input type="tel" name="phone_number" placeholder="Mobile Number" />
                        </div>
                        <div className="form-group">
                            <input type="text" name="id_number" placeholder="ID Number" />
                        </div>

                        <div className="footer">
                            <button type="submit" className="btn">
                                Register
                            </button>
                        </div>

                    </form>
                </div>

            </div>
        );
    }
}
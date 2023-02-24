import React from 'react';

import './styles/Home.scss';

function Home() {

    let profilePhotoUrl = sessionStorage.getItem("profile_photo");

    console.log(profilePhotoUrl)

    if (profilePhotoUrl === null) {
        profilePhotoUrl = "public/assets/default.jpg"
        console.log("profilePhotoUrl: " + profilePhotoUrl)
    }

    return (
        <div className="home-menu">
            <img className="profile-photo" src={profilePhotoUrl} alt={"img"}/>
            <div className="home-menu-container">
                <button className="home-menu-button" onClick={
                    () => {
                        const url = '/booked-rooms';
                        window.history.pushState(null, '', url);
                        window.location.href = url;
                    }
                }>View booked rooms</button>
                <button className="home-menu-button" onClick={
                    () => {
                        sessionStorage.clear();
                        const url = '/';
                        window.history.pushState(null, '', url);
                        window.location.href = url;
                    }
                }>Logout</button>
            </div>
        </div>
    );
}

export default Home;
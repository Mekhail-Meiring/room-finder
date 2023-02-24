import React, {useState} from "react";

const CancelBooking = () => {

    const [refundAmount, setRefundAmount] = useState(0);

    const clientId =  sessionStorage.getItem("client_id");
    const bookingId = 1

    const getRefundAmount = async (e) =>{

        e.preventDefault()

        const formData = new FormData(e.target);

        const response = await fetch('http://localhost:8080/api/cancel-booking', {
            method: 'POST',
            body : formData
        });

        const data = await response.json();
        console.log(data)

    }


    return (
        <div>
            <form onSubmit={getRefundAmount}>
                <input type={"hidden"} name={"booking_id"} value={bookingId}/>
                <input type={"hidden"} name={"client_id"} value={clientId}/>
                <button type="submit">Cancel booking</button>
            </form>
        </div>
    );
}

export default CancelBooking;
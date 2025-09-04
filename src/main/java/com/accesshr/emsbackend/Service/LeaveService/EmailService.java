package com.accesshr.emsbackend.Service.LeaveService;

import com.accesshr.emsbackend.Entity.LeaveRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;


    // Method to send a basic email
//    public void sendEmail(String to, String subject, String text) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(to); // Set recipient email address
//        message.setSubject(subject);
//        message.setText(text);
//        try {
//            mailSender.send(message);
//        }catch (Exception e) {
//            throw new RuntimeException("Error sending email",e);
//        }
//    }

//    // Method to send an email to the manager when an employee submits a leave request
//    public void sendLeaveRequestEmail(String managerEmail,LeaveRequest leaveRequest) {
//        String subject = "Leave Approval Request from "+leaveRequest.getLastName()+" "+leaveRequest.getFirstName();
//        String body="Hi Sir/Madam,\n\n"+
//                leaveRequest.getLastName()+" "+leaveRequest.getFirstName()+" has requested "+leaveRequest.getLeaveType()+" leave.\n"+
//                "Details:\n"+
//                "Employee Id: "+leaveRequest.getEmployeeId()+"\n"+
//                "Employee Email: "+leaveRequest.getEmail()+"\n"+
//                "Phone: "+leaveRequest.getPhone()+"\n"+
//                "Position: "+leaveRequest.getPosition()+"\n"+
//                "Leave Type: "+leaveRequest.getLeaveType()+"\n"+
//                "Start Date: "+leaveRequest.getLeaveStartDate()+"\n"+
//                "End Date: "+leaveRequest.getLeaveEndDate()+"\n"+
//                "Duration: "+leaveRequest.getDuration()+" Days"+"\n"+
//                "Comments: "+(leaveRequest.getComments()!=null ? leaveRequest.getComments():"N/A")+"\n\n"+
//                "Please click one of the options below:\n"+
//                "[Approve Leave](http://localhost:8080/leave/approve/"+leaveRequest.getId()+")\n"+
//                "[Reject Leave](http://localhost:8080/leave/reject/"+leaveRequest.getId()+")\n\n"+
//                "Regards,\n"+
//                leaveRequest.getLastName()+" "+leaveRequest.getFirstName();
//
//        // Send the email to the manager
//        sendEmail(managerEmail, subject, body);
//    }


    public void sendEmail(String to, String subject, String htmlContent) {
        try {
            // Create a MimeMessage
            MimeMessage message = mailSender.createMimeMessage();

            // Create a helper object to set the message details
            MimeMessageHelper helper = new MimeMessageHelper(message, true); // true indicates multipart (for HTML)

            // Set the recipient, subject, and content of the email
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true indicates the content is HTML

            // Send the email
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending email", e);
        }
    }

    public void sendRegistrationEmail(String email, String password, String schemaName) {
        String subject = "Welcome to Talentflow";

        String loginUrl = "https://talents-flow-live-server.azurewebsites.net/" + schemaName + "/login";
        System.out.println("Register Email: " + loginUrl);

        String emailContent =
                "<!DOCTYPE html>" +
                        "<html lang='en'>" +
                        "<head>" +
                        "<meta charset='UTF-8' />" +
                        "<meta name='viewport' content='width=device-width,initial-scale=1' />" +
                        "<title>Welcome to Talentflow</title>" +
                        "</head>" +
                        "<body style='margin:0;padding:0;background-color:#f3f4f6;font-family:Arial,sans-serif;color:#111827;'>" +

                        "<table role='presentation' cellpadding='0' cellspacing='0' width='100%' style='background-color:#f3f4f6;padding:32px 16px;'>" +
                        "<tr><td align='center'>" +

                        "<table role='presentation' cellpadding='0' cellspacing='0' width='100%' style='max-width:600px;background-color:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 6px 18px rgba(17,24,39,0.08);'>" +

                        // Header
                        "<tr>" +
                        "<td style='background-color:#19CF99;color:#ffffff;text-align:center;padding:20px;'>" +
                        "<h2 style='margin:0;font-size:22px;font-weight:600;'>Welcome to Talentflow</h2>" +
                        "</td>" +
                        "</tr>" +

                        // Body
                        "<tr><td style='padding:24px;'>" +

                        // Credentials Card (use table, no div)
                        "<table width='100%' cellpadding='0' cellspacing='0' style='border:1px solid #e5e7eb;border-radius:8px;background:#f9fafb;margin-bottom:16px;'>" +
                        "<tr><td style='padding:16px;'>" +
                        "<p style='margin:0 0 10px;font-size:13px;color:#6b7280;'>Username : <span style='font-size:15px;font-weight:600;color:#111827;'>" + email + "</span></p>" +
                        "<p style='margin:0;font-size:13px;color:#6b7280;'>Password : <span style='font-size:15px;font-weight:600;color:#111827;'>" + password + "</span></p>" +
                        "</td></tr></table>" +

                        // Login Button
                        "<div style='text-align:center;margin:20px 0;'>" +
                        "<a href='" + loginUrl + "' target='_blank' " +
                        "style='display:inline-block;padding:12px 20px;background-color:#19CF99;color:#ffffff;font-size:14px;font-weight:600;border-radius:8px;text-decoration:none;'>Log in to Talentflow</a>" +
                        "</div>" +

                        // Fallback URL
                        "<p style='margin:16px 0 0;font-size:12px;color:#6b7280;'>If the button doesn't work, copy and paste this URL into your browser:<br>" +
                        "<a href='" + loginUrl + "' target='_blank' style='color:#2563eb;word-break:break-all;text-decoration:none;'>" + loginUrl + "</a></p>" +

                        "</td></tr>" +

                        // Footer
                        "<tr>" +
                        "<td style='background-color:#f9fafb;padding:16px;text-align:center;color:#9ca3af;font-size:12px;'>" +
                        "<p style='margin:0;'>© 2025 Talentflow. All rights reserved.</p>" +
                        "</td>" +
                        "</tr>" +

                        "</table>" +
                        "</td></tr>" +
                        "</table>" +
                        "</body>" +
                        "</html>";

        sendEmail(email, subject, emailContent);
    }

    public void sendLeaveRequestEmail(String managerEmail, LeaveRequest leaveRequest) {
        // Set the subject of the email
        String subject = "Leave Approval Request from " + leaveRequest.getLastName() + " " + leaveRequest.getFirstName();

        // Start the email content in HTML format (no <html>, <head>, or <body> tags)
        String emailContent = "<div style=\"font-family: Arial, sans-serif; color: #333; margin: 0; padding: 0; background-color: #f4f4f4;\">" +
                "<div style=\"max-width: 600px; margin: 0 auto; background-color: #fff; padding: 20px; border-radius: 8px;\">" +

                // Header Section
                "<div style=\"text-align: center; background-color: #4CAF50; color: white; padding: 10px 0; border-radius: 5px;\">" +
                "<h2>Leave Approval Request</h2>" +
                "</div>" +

                // Request Details Section
                "<div style=\"margin-bottom: 20px;\">" +
                "<h3 style=\"color: #2c3e50; font-size: 18px;\">Request Details:</h3>" +
                "<p style=\"color: #555; font-size: 14px; line-height: 1.5;\"><strong>Employee Name:</strong> " + leaveRequest.getFirstName() + " " + leaveRequest.getLastName() + "</p>" +
                "<p style=\"color: #555; font-size: 14px; line-height: 1.5;\"><strong>Employee Id:</strong> " + leaveRequest.getEmployeeId() + "</p>" +
                "<p style=\"color: #555; font-size: 14px; line-height: 1.5;\"><strong>Email:</strong> " + leaveRequest.getEmail() + "</p>" +
                "<p style=\"color: #555; font-size: 14px; line-height: 1.5;\"><strong>Leave Type:</strong> " + leaveRequest.getLeaveSheet().getLeaveType() + "</p>" +
                "<p style=\"color: #555; font-size: 14px; line-height: 1.5;\"><strong>Start Date:</strong> " + leaveRequest.getLeaveStartDate() + "</p>" +
                "<p style=\"color: #555; font-size: 14px; line-height: 1.5;\"><strong>End Date:</strong> " + leaveRequest.getLeaveEndDate() + "</p>" +
                "<p style=\"color: #555; font-size: 14px; line-height: 1.5;\"><strong>Duration:</strong> " + leaveRequest.getDuration() + " Days</p>" +
                "<p style=\"color: #555; font-size: 14px; line-height: 1.5;\"><strong>Comments:</strong> " + (leaveRequest.getComments() != null ? leaveRequest.getComments() : "N/A") + "</p>" +
                "</div>" +

                // Action Buttons Section
                "<div style=\"margin-bottom: 20px;\">" +
                "<p style=\"color: #555; font-size: 14px; line-height: 1.5;\">Please click one of the options below to approve or reject the leave request:</p>" +
                "<a href=\"http://localhost:8080/leave/approve/" + leaveRequest.getId() + "\" style=\"display: inline-block; background-color: #4CAF50; color: white; padding: 12px 20px; text-decoration: none; border-radius: 5px; margin-right: 10px; margin-top: 10px; text-align: center; font-size: 16px; transition: background-color 0.3s;\">" +
                "Approve Leave" +
                "</a>" +
                "<a href=\"http://localhost:8080/leave/reject/" + leaveRequest.getId() + "\" style=\"display: inline-block; background-color: #f44336; color: white; padding: 12px 20px; text-decoration: none; border-radius: 5px; margin-top: 10px; text-align: center; font-size: 16px; transition: background-color 0.3s;\">" +
                "Reject Leave" +
                "</a>" +
                "</div>" +

                // Closing Section
                "<p style=\"color: #555; font-size: 14px; line-height: 1.5;\">Regards,</p>" +
                "<p style=\"color: #555; font-size: 14px; line-height: 1.5;\">" + leaveRequest.getFirstName() + " " + leaveRequest.getLastName() + "</p>" +
                "</div>" +
                "</div>" +

                // Responsive Styles
                "<style>" +
                "@media screen and (max-width: 600px) {" +
                "    .email-container {" +
                "        width: 100% !important;" +
                "        padding: 10px;" +
                "    }" +
                "    .email-header {" +
                "        font-size: 22px !important;" +
                "        padding: 8px 0 !important;" +
                "    }" +
                "    .email-body p, .email-body h3 {" +
                "        font-size: 16px !important;" +
                "    }" +
                "    .email-buttons a {" +
                "        padding: 10px 15px !important;" +
                "        font-size: 14px !important;" +
                "        margin-right: 0 !important;" +
                "        display: block;" +
                "        margin-bottom: 10px;" +
                "    }" +
                "}" +
                "</style>";

        // Ensure the email is sent with HTML content type
        sendEmail(managerEmail, subject, emailContent);
    }


//    // Method to send an email to the employee after a leave request has been approved
//    public void sendResponseToEmployee(LeaveRequest.LeaveStatus leaveStatus,LeaveRequest leaveRequest) {
//        String subject = "Leave request "+leaveStatus.name();
//        String body="Hi "+leaveRequest.getLastName()+" "+leaveRequest.getFirstName()+",\n\n"+
//                "Your leave request from "+leaveRequest.getLeaveStartDate()+" to "+leaveRequest.getLeaveEndDate()+
//                " has been "+leaveStatus.name()+".\n\nRegards,\n\n Manager";
//        sendEmail(leaveRequest.getEmail(), subject, body);
//    }
//
//    // Method to send an email to the employee after a leave request has been rejected
//    public void sendApprovalNotification(LeaveRequest.LeaveStatus leaveStatus,LeaveRequest leaveRequest) {
//
//        // Set email subject with approval/rejection status
//        String subject = "Leave approval request "+leaveStatus.name();
//        String body="Dear "+leaveRequest.getLastName()+" "+leaveRequest.getFirstName()+",\n\n"+
//                "Your leave request has been "+leaveStatus.name().toLowerCase()+".\n\n"+
//                "Reason: "+leaveRequest.getLeaveReason()+"\n\n"+
//                "If You have any questions, please contact Manager.\n\n"+
//                "Regards,\n\n Manager";
//
//        // Send the email to the employee
//        sendEmail(leaveRequest.getEmail(), subject, body);
//    }


    // Method to send an email to the employee after a leave request has been approved
    public void sendResponseToEmployee(LeaveRequest.LeaveStatus leaveStatus, LeaveRequest leaveRequest) {
        // Set the subject of the email
        String subject = "Leave request " + leaveStatus.name();

        // Create HTML email content
        String emailContent = "<div style=\"font-family: Arial, sans-serif; color: #333; margin: 0; padding: 0; background-color: #f4f4f4;\">" +
                "<div style=\"max-width: 600px; margin: 0 auto; background-color: #fff; padding: 20px; border-radius: 8px;\">" +

                // Header Section
                "<div style=\"text-align: center; background-color: #4CAF50; color: white; padding: 10px 0; border-radius: 5px;\">" +
                "<h2>Your Leave Request Status</h2>" +
                "</div>" +

                // Body Section
                "<div style=\"margin-bottom: 20px;\">" +
                "<p style=\"color: #555; font-size: 14px; line-height: 1.5;\">Hi " + leaveRequest.getFirstName() + " " + leaveRequest.getLastName() + ",</p>" +
                "<p style=\"color: #555; font-size: 14px; line-height: 1.5;\">Your leave request from <strong>" + leaveRequest.getLeaveStartDate() + "</strong> to <strong>" + leaveRequest.getLeaveEndDate() + "</strong> has been <strong>" + leaveStatus.name() + "</strong>.</p>" +
                "<p style=\"color: #555; font-size: 14px; line-height: 1.5;\">Regards,<br/> Manager</p>" +
                "</div>" +

                "</div>" +
                "</div>";

        // Ensure the email is sent with HTML content type
        sendEmail(leaveRequest.getEmail(), subject, emailContent);
    }


    // Method to send an email to the employee after a leave request has been rejected
    public void sendApprovalNotification(LeaveRequest.LeaveStatus leaveStatus, LeaveRequest leaveRequest) {
        // Set email subject with approval/rejection status
        String subject = "Leave approval request " + leaveStatus.name();

        // Create HTML email content
        String emailContent = "<div style=\"font-family: Arial, sans-serif; color: #333; margin: 0; padding: 0; background-color: #f4f4f4;\">" +
                "<div style=\"max-width: 600px; margin: 0 auto; background-color: #fff; padding: 20px; border-radius: 8px;\">" +

                // Header Section
                "<div style=\"text-align: center; background-color: #f44336; color: white; padding: 10px 0; border-radius: 5px;\">" +
                "<h2>Your Leave Request Status</h2>" +
                "</div>" +

                // Body Section
                "<div style=\"margin-bottom: 20px;\">" +
                "<p style=\"color: #555; font-size: 14px; line-height: 1.5;\">Dear " + leaveRequest.getFirstName() + " " + leaveRequest.getLastName() + ",</p>" +
                "<p style=\"color: #555; font-size: 14px; line-height: 1.5;\">Your leave request has been <strong>" + leaveStatus.name().toLowerCase() + "</strong>.</p>" +
                "<p style=\"color: #555; font-size: 14px; line-height: 1.5;\">Reason: <strong>" + leaveRequest.getLeaveReason() + "</strong></p>" +
                "<p style=\"color: #555; font-size: 14px; line-height: 1.5;\">If you have any questions, please contact your Manager.</p>" +
                "<p style=\"color: #555; font-size: 14px; line-height: 1.5;\">Regards,<br/> Manager</p>" +
                "</div>" +

                "</div>" +
                "</div>";

        // Ensure the email is sent with HTML content type
        sendEmail(leaveRequest.getEmail(), subject, emailContent);
    }


}

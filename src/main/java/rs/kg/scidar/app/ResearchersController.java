package rs.kg.scidar.app;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@CrossOrigin(origins = "http://147.91.204.111:4000", maxAge = 3600, allowCredentials = "true")
//@CrossOrigin(origins = "https://dspace.unic.kg.ac.rs", maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping(path = "/researchers")
public class ResearchersController {

    @Value("${FRONTEND_URL}")
    public String FRONTEND_URL;

    @Value("${CSV_URL}")
    public String CSV_URL;

    @Value("${SOLR_URL}")
    public String SOLR_URL;

    @Value("${SMTP_SERVER}")
    public String SMTP_SERVER;

    @Value("${SMTP_PORT}")
    public int SMTP_PORT;

    @Value("${SMTP_USER}")
    public String SMTP_USER;

    @Value("${SMTP_PASSWORD}")
    public String SMTP_PASSWORD;

    @Value("${MAIL_TO}")
    public String MAIL_TO;

    @Value("${REFRESH_CSV_MINS}")
    public int REFRESH_CSV_MINS;

    private AuthorityCSVContent csv;

    @PostConstruct
    public void init() {
        this.csv = new AuthorityCSVContent(CSV_URL, SOLR_URL, REFRESH_CSV_MINS);
    }

    /**
     *
     * @param authority
     * @return
     * @throws JSONException
     */
    @GetMapping(path = "/getresearcher/{authority}", produces = "application/json")
    public ResponseEntity getResearchers(@PathVariable("authority") String authority) throws JSONException {

        JSONObject json = new JSONObject();

        String orcid = csv.findOrcidByAuthority(authority);

        if (!orcid.equals("")) {
            json.put("status", true);
            json.put("authority", authority);
            json.put("orcid", orcid);

            Person p = csv.findByOrcid(orcid);
            if (p != null) {
                json.put("name", p.getName());
            }
            if (p != null) {
                json.put("scopus", p.getScopusID());
            }
            if (p != null) {
                json.put("ecris", p.getEcrisID());
            }
        } else {
            json.put("status", false);
            json.put("authority", authority);
        }

        return ResponseEntity.ok(json.toString());
    }

    /**
     *
     * @param orcid
     * @return
     * @throws JSONException
     */
    @GetMapping(path = "/getresearcherbyorcid/{orcid}", produces = "application/json")
    public ResponseEntity getResearcherByORCID(@PathVariable("orcid") String orcid) throws JSONException {

        JSONObject json = new JSONObject();

        Person p = csv.findByOrcid(orcid);

        if (p != null) {
            json.put("status", true);
            json.put("orcid", orcid);

            json.put("name", p.getName());
            json.put("scopus", p.getScopusID());
            json.put("ecris", p.getEcrisID());
            json.put("authority", csv.findAuthorityByOrcid(orcid));
        } else {
            json.put("status", false);
            json.put("orcid", orcid);
        }

        return ResponseEntity.ok(json.toString());
    }

    /**
     *
     * @param orcid
     * @return
     * @throws JSONException
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/publicationsbyorcid/{orcid}", method = RequestMethod.GET)
    public ModelAndView publicationsByORCID(@PathVariable("orcid") String orcid) throws JSONException, UnsupportedEncodingException {

        Person p = csv.findByOrcid(orcid);
        String authority = csv.findAuthorityByOrcid(orcid);

        if (!authority.equals("")) {
            String ime = URLEncoder.encode(p.getName(), StandardCharsets.UTF_8.toString());
            return new ModelAndView("redirect:" + FRONTEND_URL + "/browse/author?value=" + ime + "&authority=" + authority);
        } else {
            return new ModelAndView("redirect:" + FRONTEND_URL + "/browse/author");
        }

    }

    /**
     *
     * @param input
     * @return
     * @throws JSONException
     * @throws UnsupportedEncodingException
     */
    @PostMapping("/reporterrorinitem")
    public ResponseEntity ReportErrorInItem(@RequestBody ScidarEmail input) throws JSONException, UnsupportedEncodingException {

        String note = input.getNote().replace("\n", "<br/>");

        Email email = EmailBuilder.startingBlank()
                .to("SCIDAR Admin", MAIL_TO)
                .from(input.getEmail())
                .cc(input.getEmail())
                .withReplyTo(input.getEmail())
                .withSubject("SCIDAR Primedba: " + input.getTitle())
                .withHTMLText("<b>Naslov:</b><br/>" + input.getTitle()
                        + "<br/><br/><b>URI:</b> <br/> <a href=\"" + input.getUri() + "\">" + input.getUri()
                        + "</a><br/><br/><b>Primedba: </b><br/>" + note
                        + "<br/><br/>Pozdrav,<br/>" + input.getName())
                .buildEmail();

        Mailer mailer = MailerBuilder
                .withSMTPServer(SMTP_SERVER, SMTP_PORT, SMTP_USER, SMTP_PASSWORD)
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .withSessionTimeout(10 * 1000)
                .clearEmailValidator() // turns off email validation
                .withDebugLogging(true)
                .async()
                .buildMailer();

        mailer.sendMail(email);

        return ResponseEntity.ok(input);
    }

}

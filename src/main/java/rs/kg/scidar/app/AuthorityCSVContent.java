package rs.kg.scidar.app;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

public class AuthorityCSVContent {

    private static final Logger log = Logger.getLogger(AuthorityCSVContent.class);

    private final HashMap<String, Person> persons = new HashMap<>();
    private final String csvURL;
    private final String solrURL;
    private final Timer timer;

    public HashMap<String, Person> getPersons() {
        return persons;
    }

    // Konstruktor ucitava CSV
    public AuthorityCSVContent(String csvURL, String solrURL, int refreshCSVmins) {
        this.csvURL = csvURL;
        this.solrURL = solrURL;

        timer = new Timer();
        timer.scheduleAtFixedRate(new TaskOsvezavanje(), 10, refreshCSVmins*60*1000);

        //loadContent();
    }

    class TaskOsvezavanje extends TimerTask {
        @Override
        public void run() {
            loadContent();
            //timer.cancel();
        }
    }

    public String findOrcidByAuthority(String authority) {

        HttpSolrClient solr = new HttpSolrClient.Builder(solrURL).build();
        solr.setParser(new XMLResponseParser());

        SolrQuery query = new SolrQuery();
        query.set("q", "id:" + authority);

        QueryResponse response = null;
        try {
            response = solr.query(query);
        } catch (SolrServerException | IOException ex) {
            java.util.logging.Logger.getLogger(AuthorityCSVContent.class.getName()).log(Level.SEVERE, null, ex);
        }

        SolrDocumentList docList = response.getResults();

        if (!docList.isEmpty() && docList.get(0).containsKey("orcid_id")) {
            return (String) docList.get(0).getFieldValue("orcid_id");
        } else {
            return "";
        }
    }

    public String findAuthorityByOrcid(String orcid) {

        HttpSolrClient solr = new HttpSolrClient.Builder(solrURL).build();
        solr.setParser(new XMLResponseParser());

        SolrQuery query = new SolrQuery();
        query.set("q", "orcid_id:" + orcid);

        QueryResponse response = null;
        try {
            response = solr.query(query);
        } catch (SolrServerException | IOException ex) {
            java.util.logging.Logger.getLogger(AuthorityCSVContent.class.getName()).log(Level.SEVERE, null, ex);
        }

        SolrDocumentList docList = response.getResults();

        if (!docList.isEmpty() && docList.get(0).containsKey("id")) {
            return (String) docList.get(0).getFieldValue("id");
        } else {
            return "";
        }
    }

    // Ucitavanje CSV-a
    private void loadContent() {

        InputStream in;
        String row;
        BufferedReader csvReader;
        
        persons.clear();

        try {
            in = new URL(csvURL).openStream();
            csvReader = new BufferedReader(new InputStreamReader(in));

            // Preskoci prvu liniju
            csvReader.readLine();

            while ((row = csvReader.readLine()) != null) {

                // Skloni navodnike
                row = row.replace("\"", "");

                String[] data = row.split(",");

                //id	orcid	e-cris	scopus	PrezimeIme	mail	Fakultet	Katedra
                if (data.length == 8) {
                    persons.put(data[1].trim(), new Person(data[1], data[2], data[3], data[6], data[4], data[5], data[7], data[6], data[4]));
                } else if (data.length == 7) // Ako nedostaje katedra, dodaj prazan string
                {
                    persons.put(data[1].trim(), new Person(data[1], data[2], data[3], data[6], data[4], data[5], "", data[6], data[4]));
                }

                //String orcidID,  String ecrisID, String scopusID, String organisationID, String name, String email, String organisationPart,  String organisationName
                //System.out.println(persons.get(data[1].trim()));
            }
            csvReader.close();
        } catch (MalformedURLException ex) {
            java.util.logging.Logger.getLogger(AuthorityCSVContent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(AuthorityCSVContent.class.getName()).log(Level.SEVERE, null, ex);
        }

        log.debug("Ucitao sam osobe..." + String.valueOf(persons.size()));
    }

    private String prepareSearchTerm(String term) {
        String tmp = SearchUtil.replaceChars(term);
        String[] names = tmp.split(" ");
        for (int i = 0; i < names.length; i++) {
            names[i] = StringUtils.capitalize(names[i]);
        }
        return String.join(" ", names);
    }

    public Person findByOrcid(String orcid) {
        if (persons.containsKey(orcid)) {
            return persons.get(orcid);
        }
        return null;
    }

    public List<Person> findByName(String name) {
        log.debug("Stiglo mi je: " + name + " trazim: " + prepareSearchTerm(name));

        List<Person> matches = new ArrayList<>();
        if (name.length() > 0) {
            for (var p : persons.entrySet()) {
                log.debug("##trazim sam : " + p.getValue().getName() + " " + p.getValue().getSearchIndex());

                if (StringUtils.containsIgnoreCase(p.getValue().getSearchIndex(), prepareSearchTerm(name))) //                if (p.getValue().getSearchIndex().compareToIgnoreCase(prepareSearchTerm(name)) == 0)
                {
                    matches.add(p.getValue());
                    log.debug("nasao sam : " + p.getValue().getName() + " " + p.getValue().getOrcidID());
                }
            }
        }
        return matches;
    }

    public List<String> findOrcidByName(String name) {
        List<String> orcid = new ArrayList<>();
        List<Person> osobe = findByName(name);
        if (!osobe.isEmpty()) {
            for (Person element : osobe) {
                orcid.add(element.getOrcidID());
            }
        }
        return orcid;
    }

    public String generateLink(String orcid) throws IOException {
        String returnVal = "";
        if (orcid.length() == 19) {
            returnVal = "<a target=\"_blank\" href=\"https://orcid.org/" + URLEncoder.encode(orcid, "UTF-8")
                    + "\"> <img style=\"height:15px;padding-left:5px\" src=\"/jspui/image/orcid.png\">  </a>";

            Person p = findByOrcid(orcid);
            if (p != null && !p.getOrganisationID().isEmpty()) {
                returnVal = returnVal + "<a target=\"_blank\" href=\"https://www.kg.ac.rs/nastavnici_nastavnik.php?ib_je=" + URLEncoder.encode(p.getOrganisationID(), "UTF-8")
                        + "\"> <img style=\"height:15px;padding-left:5px\" src=\"/jspui/image/unikg.png\"></a>";

//                returnVal = returnVal + "<a target=\"_blank\" href=\"https://e-cris.sr.cobiss.net/public/jqm/search_basic.aspx?lang=scr&opdescr=rsrSearch&opt=2&subopt=3&code1=rsr&code2=nameadvanced&search_term=name="
                returnVal = returnVal + "<a target=\"_blank\" href=\"https://cris.cobiss.net/ecris/sr/sr_latn/researcher/code/"
                        + URLEncoder.encode(p.getOrganisationID(), "UTF-8")
                        + "\"> <img style=\"height:15px;padding-left:5px\" src=\"/jspui/image/cris-logo.svg\"></a>";
//                        + "\"> <img style=\"height:15px;padding-left:5px\" src=\"/jspui/image/ecris.png\"></a>";

                if (!p.getScopusID().isEmpty()) {
                    returnVal = returnVal + "<a target=\"_blank\" href=\"https://www.scopus.com/authid/detail.uri?authorId="
                            + URLEncoder.encode(p.getScopusID(), "UTF-8")
                            + "\"> <img style=\"height:15px;padding-left:5px\" src=\"/jspui/image/sc.svg\"></a>";
                }
            }
        }
        return returnVal;
    }

}

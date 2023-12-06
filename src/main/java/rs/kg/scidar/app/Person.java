package rs.kg.scidar.app;

public class Person {

    private String orcidID;
    private String organisationID;
    private String name;
    private String email;
    private String organisationPart;
    private String organisationName;
    private String searchIndex;
    private String scopusID;
    private String ecrisID;

    public Person(String orcidID, String ecrisID, String scopusID, String organisationID, String name, String email, String organisationPart, String organisationName, String searchIndex) {
        this.orcidID = orcidID.trim();
        this.organisationID = organisationID.trim();
        this.name = name.trim();
        this.organisationName = organisationName.trim();
        this.searchIndex = searchIndex.trim();
        this.email = email.trim();
        this.organisationPart = organisationPart.trim();
        this.scopusID = scopusID.trim();
        this.ecrisID = ecrisID.trim();
    }

    public void setOrcidID(String orcidID) {
        this.orcidID = orcidID;
    }

    public void setScopusID(String scopusID) {
        this.scopusID = scopusID;
    }

    public void setEcrisID(String ecrisID) {
        this.ecrisID = ecrisID;
    }

    public void setOrganisationID(String organisationID) {
        this.organisationID = organisationID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }

    public void setSearchIndex(String searchIndex) {
        this.searchIndex = searchIndex;
    }

    public String getOrcidID() {
        return orcidID;
    }

    public String getScopusID() {
        return scopusID;
    }

    public String getEcrisID() {
        return ecrisID;
    }

    public String getOrganisationID() {
        if (organisationID.equalsIgnoreCase("0")) {
            return "";
        }
        return organisationID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOrganisationPart() {
        return organisationPart;
    }

    public void setOrganisationPart(String organisationPart) {
        this.organisationPart = organisationPart;
    }

    public String getName() {
        return name;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public String getSearchIndex() {
        return searchIndex;
    }

    @Override
    public String toString() {
        return "ORCID: " + orcidID
                + " Organisation: " + organisationID
                + " Name: " + name
                + " Email: " + email
                + " OrganisationPart: " + organisationPart
                + " OrganisationName: " + organisationName
                + " searchIndex: " + searchIndex
                + " ScopusID: " + scopusID
                + " ECrisID: " + ecrisID;
    }

}

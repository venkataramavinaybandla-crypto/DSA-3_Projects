package core;

/**
 * Domain model representing a directed citation relationship from a citing paper to a cited paper.
 */
public class Citation {
    private String sourcePaperId;
    private String targetPaperId;

    public Citation() {
        this.sourcePaperId = "";
        this.targetPaperId = "";
    }

    public Citation(String sourcePaperId, String targetPaperId) {
        this.sourcePaperId = sourcePaperId;
        this.targetPaperId = targetPaperId;
    }

    public String getSourcePaperId() {
        return sourcePaperId;
    }

    public void setSourcePaperId(String sourcePaperId) {
        this.sourcePaperId = sourcePaperId;
    }

    public String getTargetPaperId() {
        return targetPaperId;
    }

    public void setTargetPaperId(String targetPaperId) {
        this.targetPaperId = targetPaperId;
    }

    public String getCitingPaperId() {
        return sourcePaperId;
    }

    public void setCitingPaperId(String citingPaperId) {
        this.sourcePaperId = citingPaperId;
    }

    public String getCitedPaperId() {
        return targetPaperId;
    }

    public void setCitedPaperId(String citedPaperId) {
        this.targetPaperId = citedPaperId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Citation other = (Citation) obj;
        boolean sourceEqual = (this.sourcePaperId == null) ? (other.sourcePaperId == null) : this.sourcePaperId.equals(other.sourcePaperId);
        boolean targetEqual = (this.targetPaperId == null) ? (other.targetPaperId == null) : this.targetPaperId.equals(other.targetPaperId);
        return sourceEqual && targetEqual;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + (sourcePaperId != null ? sourcePaperId.hashCode() : 0);
        hash = 31 * hash + (targetPaperId != null ? targetPaperId.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "Citation{sourcePaperId='" + sourcePaperId + "', targetPaperId='" + targetPaperId + "'}";
    }
}

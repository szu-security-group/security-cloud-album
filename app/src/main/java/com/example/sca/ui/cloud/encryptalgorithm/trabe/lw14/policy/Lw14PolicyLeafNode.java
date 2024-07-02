package com.example.sca.ui.cloud.encryptalgorithm.trabe.lw14.policy;

import java.io.IOException;

import it.unisa.dia.gas.jpbc.Pairing;
import com.example.sca.ui.cloud.encryptalgorithm.trabe.AbeOutputStream;
import com.example.sca.ui.cloud.encryptalgorithm.trabe.AbePrivateKey;
import com.example.sca.ui.cloud.encryptalgorithm.trabe.AbePublicKey;
import com.example.sca.ui.cloud.encryptalgorithm.trabe.AbeSettings;
import com.example.sca.ui.cloud.encryptalgorithm.trabe.lw14.Lw14PrivateKeyComponent;
import com.example.sca.ui.cloud.encryptalgorithm.trabe.lw14.Lw14Util;
import it.unisa.dia.gas.jpbc.Element;

public class Lw14PolicyLeafNode extends Lw14PolicyAbstractNode {
    private Lw14PrivateKeyComponent satisfyingComponent = null;
    /** G1 **/
    private Element hashedAttribute;
    /** G1 **/
    private Element p1;
    /** G1 **/
    private Element p2;
    /** G1 **/
    private Element p3;

    protected Lw14PolicyLeafNode(Element hashedAttribute, Element p1, Element p2, Element p3) {
        this(hashedAttribute);
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }

    public Lw14PolicyLeafNode(Element hashedAttribute) {
        this.hashedAttribute = hashedAttribute;
    }

    public Lw14PolicyLeafNode(String attribute, AbePublicKey publicKey) {
        hashedAttribute = Lw14Util.elementZrFromString(attribute, publicKey);
    }

    public int getThreshold() {
        return 1;
    }

    private Element getHashedAttribute() {
        return hashedAttribute;
    }

    @Override
    public void writeToStream(AbeOutputStream stream) throws IOException {
        stream.writeInt(getThreshold());
        stream.writeInt(0);
        stream.writeElement(hashedAttribute);
        stream.writeElement(p1);
        stream.writeElement(p2);
        stream.writeElement(p3);
    }

    @Override
    public void fillPolicy(AbePublicKey pub, Element e) {
        Element b = pub.getPairing().getZr().newRandomElement();

        p1 = pub.f.duplicate().powZn(e)
                .mul(pub.G.duplicate().powZn(b));
        p2 = pub.H.duplicate().powZn(hashedAttribute)
                .mul(pub.h).powZn(b.duplicate().negate());
        p3 = pub.g.duplicate().powZn(b);
    }

    @Override
    public void fillPolicy(AbePublicKey pub, Element e, Lw14TreePreprocessing tpp) {
        Element b = pub.getPairing().getZr().newRandomElement();

        p1 = tpp.eppp_f.powZn(e)
                .mul(tpp.eppp_G.powZn(b));
        p2 = tpp.eppp_H.powZn(hashedAttribute)
                .mul(pub.h).powZn(b.duplicate().negate());
        p3 = tpp.eppp_g.powZn(b);
    }

    @Override
    protected boolean checkSatisfySpecific(AbePrivateKey prv) {
    	satisfyingComponent = prv.getSatisfyingComponent(getHashedAttribute());
    	return satisfyingComponent != null;
    }

    @Override
    public void pickSatisfyMinLeaves(AbePrivateKey prv) {
        minLeaves = 1;
    }

    @Override
    protected void decFlattenSpecific(Element r, Element exp, AbePrivateKey prv) {
        Pairing p = prv.getPublicKey().getPairing();
        Element c = null;
        if (AbeSettings.PREPROCESSING) {
            if (prv.k2_ij_pp == null) {
                prv.k2_ij_pp = p.getPairingPreProcessingFromElement(prv.k2_ij);
            }
            c = prv.k2_ij_pp.pairing(p1);
        } else {
            c = p.pairing(prv.k2_ij, p1);
        }
        r.mul(c.mul(p.pairing(satisfyingComponent.k1_ijx, p2))
                .mul(p.pairing(satisfyingComponent.k2_ijx, p3))
                .powZn(exp));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Lw14PolicyLeafNode)) {
            return false;
        } else if(this == obj) {
            return true;
        }

        Lw14PolicyLeafNode leaf = (Lw14PolicyLeafNode)obj;

        boolean result = hashedAttribute.equals(leaf.getHashedAttribute());
        result = result & p1.equals(leaf.p1);
        result = result & p2.equals(leaf.p2);
        result = result & p3.equals(leaf.p3);

        return result;
    }
}

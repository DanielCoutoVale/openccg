///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2004 University of Edinburgh (Michael White)
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////

package opennlp.ccg.ngrams;

import opennlp.ccg.lexicon.*;
import opennlp.ccg.util.*;

import java.util.*;
import java.io.*;

import gnu.trove.*;

/**
 * AN n-gram filter that detects "a" followed by a word beginning with a vowel, 
 * or "an" followed by a word beginning with a consonant.
 * Note that this filter only makes an approximate check, which may be augmented 
 * with a set of exceptions. 
 * Exceptions may be culled from a file of bigrams using cullAAnExceptions, 
 * which may be accessed from the command line using the -c option.
 * An appropriate bigrams file can be produced using the SRILM ngram-count tool, 
 * with the -text and -write2 options.
 *
 * @author      Michael White
 * @version     $Revision: 1.8 $, $Date: 2011/03/20 20:11:58 $
 */
public class AAnFilter implements NgramFilter, Reversible
{
    // exceptions
    private Set<List<Association>> exceptions = null;
    
    
    /** Constructor. */
    public AAnFilter() {}
    
    /** Constructor that loads a/an exceptions from the given infile of bigrams. */
    public AAnFilter(String infile) throws IOException { 
        loadAAnExceptions(infile); 
    }
    

    /** Flag for whether to reverse words before filtering. */
    protected boolean reverse = false;
    
    /** Get reverse flag. */
    public boolean getReverse() { return reverse; }
    
    /** Set reverse flag. */
    public void setReverse(boolean reverse) { this.reverse = reverse; }
    

    /** Returns whether to filter out the given word sequence. */
    public boolean filterOut(List<Association> words) {
        for (int i = 0; i < words.size()-1; i++) {
            Association w1 = words.get(i);
            Association w2 = words.get(i+1);
            if (filterOut(w1, w2)) return true;
        }
        return false; 
    }
    
    /** Returns whether to filter out the given word bigram (reversed if apropos). */
    public boolean filterOut(Association w1, Association w2) {
        if (reverse) {
            Association tmp = w1; w1 = w2; w2 = tmp;
        }
        String f1 = w1.getForm(); 
        if (f1 != "a" && f1 != "an") return false;
        String f1Alt = (f1 == "a") ? "an" : "a";
        String f2 = w2.getForm();
        boolean defaultRetval = filterOutByDefault(f1, f2);
        if (isException(f1, f2) || isException(f1Alt, f2))
            return !defaultRetval;
        else
            return defaultRetval;
    }
    
    // returns whether to filter out the bigram by default
    private static boolean filterOutByDefault(String w1, String w2) {
        boolean w2StartsWithVowel = startsWithVowel(w2);
        return (w1 == "a" && w2StartsWithVowel) || (w1 == "an" && !w2StartsWithVowel);
    }
    
    // initial vowel
    private static boolean startsWithVowel(String word) {
        char c = word.charAt(0);
        return (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u') ||
               (c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U');
    }
    
    // words for a/an
    private static final Association A_WORD = WordPool.createWord("a");
    private static final Association AN_WORD = WordPool.createWord("an");
    
    // reusable list for lookup
    private List<Association> keyList = new ArrayListWithIdentityEquals<Association>(2);
    
    // looks up whether the bigram is an exception
    private boolean isException(String w1, String w2) {
        if (exceptions == null) return false;
        keyList.clear();
        keyList.add((w1 == "a") ? A_WORD : AN_WORD);
        keyList.add(WordPool.createWord(w2));
        return exceptions.contains(keyList);
    }
    
    // singletons for a/an
    @SuppressWarnings("unchecked")
	private static final List<Association> A_SINGLETON = (List<Association>) Interner.globalIntern(new SingletonList<Association>(A_WORD));
    @SuppressWarnings("unchecked")
    private static final List<Association> AN_SINGLETON = (List<Association>) Interner.globalIntern(new SingletonList<Association>(AN_WORD));
    
    /** Adds an a/an bigram as an exception. */
    @SuppressWarnings("unchecked")
    public void addException(String w1, String w2) {
        // make sure w1 is a/an
        w1 = w1.intern();
        if (w1 != "a" && w1 != "an") {
            System.err.println("Warning: ignoring exception not starting with a/an: " + w1 + " " + w2);
            return;
        }
        // ensure exceptions initialized
        if (exceptions == null) exceptions = new THashSet();
        // intern and add bigram
        List<Association> w1Singleton = (w1 == "a") ? A_SINGLETON : AN_SINGLETON;
        List<Association> w2Singleton = (List<Association>) Interner.globalIntern(new SingletonList<Association>(WordPool.createWord(w2)));
        List<Association> excBigram = (List<Association>) Interner.globalIntern(new StructureSharingList<Association>(w1Singleton, w2Singleton));
        exceptions.add(excBigram);
    }

    
    /** Culls a/an exceptions from the given infile of bigrams, writing them to the given outfile. */
    public static void cullAAnExceptions(String infile, String outfile) throws IOException {
        Reader in = new BufferedReader(new FileReader(infile));
        StreamTokenizer tokenizer = NgramScorer.initTokenizer(in);
        PrintWriter out = new PrintWriter(new FileWriter(outfile));
        String[] tokens = new String[2];
        // loop through lines
        while (tokenizer.ttype != StreamTokenizer.TT_EOF) {
            // read line into tokens
            NgramScorer.readLine(tokenizer, tokens);
            // check for blank/incomplete line
            if (tokens[1] == null) continue;
            // check for a/an exception
            if (tokens[0].equals("a") || tokens[0].equals("an")) {
                String aan = tokens[0].intern();
                String word = tokens[1];
                if (filterOutByDefault(aan, word)) {
                    // write to exceptions file
                    out.println(aan + " " + word);
                }
            }
        }
        // done
        in.close();
        out.flush(); out.close();
    }
    

    /** Loads a/an exceptions from the given infile of bigrams. */
    public void loadAAnExceptions(String infile) throws IOException {
        Reader in = new BufferedReader(new FileReader(infile));
        StreamTokenizer tokenizer = NgramScorer.initTokenizer(in);
        String[] tokens = new String[2];
        // loop through lines
        while (tokenizer.ttype != StreamTokenizer.TT_EOF) {
            // read line into tokens
            NgramScorer.readLine(tokenizer, tokens);
            // check for blank/incomplete line
            if (tokens[1] == null) continue;
            // add a/an exception
            addException(tokens[0], tokens[1]);
        }
        // done
        in.close();
    }

    
    /** Test loading and filtering, or cull exceptions from bigrams. */
    public static void main(String[] args) throws IOException {
        
        String usage = "Usage: java opennlp.ccg.ngrams.AAnFilter (<exceptionsfile>) <tokens> | -c <bigramsfile> <exceptionsfile>";
        
        if (args.length > 0 && args[0].equals("-h")) {
            System.out.println(usage);
            System.exit(0);
        }
        
        // cull exceptions with -c
        if (args[0].equals("-c")) {
            String infile = args[1]; String outfile = args[2];
            System.out.println("Culling a/an exceptions from " + infile + " to " + outfile);
            cullAAnExceptions(infile, outfile);
            System.exit(0);
        }
        
        // otherwise optionally load exceptions, ... 
        AAnFilter aanFilter = new AAnFilter();
        String infile = null; String tokens = null;
        if (args.length >= 2) { infile = args[0]; tokens = args[1]; }
        else tokens = args[0];
        if (infile != null) {
            System.out.println("Loading exceptions from: " + infile);
            System.out.println();
            aanFilter.loadAAnExceptions(infile);
        }
        
        // then filter given tokens
        Tokenizer tokenizer = new DefaultTokenizer();
        List<Association> words = tokenizer.tokenize(tokens); //, true);
        System.out.println("filtering: " + tokens);
        System.out.println("filter out: " + aanFilter.filterOut(words));
    }
}

